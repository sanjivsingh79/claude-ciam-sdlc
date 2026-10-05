from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import re

HOST = "127.0.0.1"
PORT = 8091

PRODUCTION_CUSTOMERS = {
    "TEST-PROD-001": {
        "customerId": "TEST-PROD-001",
        "email": "prod-customer-001@example.test",
        "status": "ACTIVE"
    }
}

ALLOWED_CLIENTS = {
    "approved-ciam-service"
}


class ProductionHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        client_identity = self.headers.get("X-Client-Identity")

        if client_identity not in ALLOWED_CLIENTS:
            self.send_response(403)
            self.send_header("Content-Type", "application/json")
            self.end_headers()

            response = {
                "error": "Production resource access denied",
                "client": client_identity
            }

            self.wfile.write(json.dumps(response).encode())
            return

        match = re.fullmatch(r"/customer/(.+)", self.path)

        if not match:
            self.send_response(404)
            self.end_headers()
            return

        customer_id = match.group(1)
        customer = PRODUCTION_CUSTOMERS.get(customer_id)

        if not customer:
            self.send_response(404)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(
                json.dumps({"error": "Customer not found"}).encode()
            )
            return

        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()

        self.wfile.write(json.dumps(customer).encode())

    def log_message(self, format, *args):
        print(f"[PRODUCTION] {self.address_string()} - {format % args}")


print(f"Mock PRODUCTION service listening on http://{HOST}:{PORT}")
print(f"Allowed clients: {sorted(ALLOWED_CLIENTS)}")

server = HTTPServer((HOST, PORT), ProductionHandler)
server.serve_forever()
