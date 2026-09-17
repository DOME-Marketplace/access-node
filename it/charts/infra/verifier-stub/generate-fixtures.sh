#!/usr/bin/env bash
#
# Regenerates the static verifier fixtures served by the verifier-stub chart.
#
# The stub stands in for the DOME verifier so the two-node replication harness can complete
# the M2M auth chain offline. Desmos mints a token at POST /token and validates it against
# GET /jwks, comparing the token's "iss" against its own configured VERIFIER_URL -- so the
# URL below is baked into the signature and must match the Service address exactly.
#
# Only the public JWK and the signed token are committed; the private key is a throwaway that
# exists purely to sign this fixture and is discarded when this script exits.
#
# Usage:  ./generate-fixtures.sh          # from within it/charts/infra/verifier-stub/
#
set -euo pipefail

VERIFIER_URL="http://verifier-stub.infra.svc.cluster.local:9595"
KEY_ID="it-verifier-key"
SUBJECT="it-client"
# Far-future expiry: the fixture is static and committed, so it must not rot. The /token
# response still advertises expires_in=3600 to match the real verifier's contract exactly --
# a client that re-fetches hourly simply gets this same token back.
EXPIRES_AT=2051222400   # 2035-01-01T00:00:00Z

cd "$(dirname "$0")"
OUT="files"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

openssl ecparam -name prime256v1 -genkey -noout -out "$TMP/ec-private.pem" 2>/dev/null
openssl ec -in "$TMP/ec-private.pem" -pubout -outform DER -out "$TMP/ec-public.der" 2>/dev/null

python3 - "$TMP" "$OUT" "$VERIFIER_URL" "$KEY_ID" "$SUBJECT" "$EXPIRES_AT" <<'PYEOF'
import base64, json, subprocess, sys, time, uuid, os

tmp, out, url, kid, sub, exp = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5], int(sys.argv[6])

def b64u(raw: bytes) -> str:
    return base64.urlsafe_b64encode(raw).rstrip(b"=").decode()

# An uncompressed P-256 point is the trailing 65 bytes of the SPKI DER: 0x04 || X(32) || Y(32).
der = open(os.path.join(tmp, "ec-public.der"), "rb").read()
point = der[-65:]
assert point[0] == 0x04, "expected an uncompressed EC point"
x, y = point[1:33], point[33:65]

jwk = {"kty": "EC", "crv": "P-256", "x": b64u(x), "y": b64u(y),
       "use": "sig", "alg": "ES256", "kid": kid}

now = int(time.time())
header = {"alg": "ES256", "kid": kid, "typ": "JWT"}
claims = {"iss": url, "sub": sub, "aud": url, "iat": now, "exp": exp,
          "jti": str(uuid.uuid4())}

def seg(obj):
    return b64u(json.dumps(obj, separators=(",", ":"), sort_keys=True).encode())

signing_input = f"{seg(header)}.{seg(claims)}".encode()

der_sig = subprocess.run(
    ["openssl", "dgst", "-sha256", "-sign", os.path.join(tmp, "ec-private.pem")],
    input=signing_input, capture_output=True, check=True).stdout

# JWS needs the raw R||S pair, but openssl emits a DER SEQUENCE of two INTEGERs. Unpack it,
# stripping the sign-padding byte DER adds to values whose high bit is set.
def der_to_raw(sig: bytes) -> bytes:
    assert sig[0] == 0x30
    body = sig[2:] if sig[1] < 0x80 else sig[2 + (sig[1] & 0x7F):]
    vals, off = [], 0
    for _ in range(2):
        assert body[off] == 0x02
        length = body[off + 1]
        vals.append(int.from_bytes(body[off + 2:off + 2 + length], "big"))
        off += 2 + length
    return b"".join(v.to_bytes(32, "big") for v in vals)

token = signing_input.decode() + "." + b64u(der_to_raw(der_sig))

os.makedirs(out, exist_ok=True)
def write(name, obj):
    with open(os.path.join(out, name), "w") as fh:
        json.dump(obj, fh, indent=2)
        fh.write("\n")

# Deliberately minimal: these are the only two fields Desmos reads from discovery.
write("openid-configuration.json", {"token_endpoint": f"{url}/token", "jwks_uri": f"{url}/jwks"})
write("jwks.json", {"keys": [jwk]})
write("token.json", {"access_token": token, "token_type": "Bearer", "expires_in": "3600"})

print(f"regenerated fixtures in {out}/ for {url}")
PYEOF
