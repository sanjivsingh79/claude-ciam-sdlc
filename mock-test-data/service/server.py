from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path
import json
from urllib.parse import urlparse, parse_qs

DATA_FILE = Path(__file__).parent.parent / "test-data.json"


def load_data():
    with open(DATA_FILE, "r", encoding="utf-8") as f:
        return json.load(f)


class Handler(BaseHTTPRequestHandler):

    def send_json(self, status, payload):
        body = json.dumps(payload).encode("utf-8")

        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        parsed = urlparse(self.path)

        if parsed.path != "/test-customers":
            self.send_json(404, {"error": "Not found"})
            return

        params = parse_qs(parsed.query)

        scenario = params.get("scenario", [None])[0]
        status = params.get("status", [None])[0]
        mfa_enabled = params.get("mfaEnabled", [None])[0]

        customers = load_data()["customers"]

        if scenario:
            customers = [
                c for c in customers
                if c["scenario"] == scenario
            ]

        if status:
            customers = [
                c for c in customers
                if c["status"] == status
            ]

        if mfa_enabled is not None:
            required = mfa_enabled.lower() == "true"

            customers = [
                c for c in customers
                if c["mfaEnabled"] == required
            ]

        self.send_json(
            200,
            {
                "count": len(customers),
                "customers": customers
            }
        )

    def log_message(self, format, *args):
        print(format % args)


if __name__ == "__main__":
    server = HTTPServer(("127.0.0.1", 8090), Handler)

    print("Mock Test Data Service running on http://127.0.0.1:8090")
    print("Press Ctrl+C to stop.")

    server.serve_forever()