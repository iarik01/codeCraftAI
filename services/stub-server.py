import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer


SERVICE_NAME = os.getenv("SERVICE_NAME", "stub-service")
PORT = int(os.getenv("PORT", "8080"))


class StubHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        payload = {
            "service": SERVICE_NAME,
            "status": "ok",
            "message": "CodeCrafters AI local development stub",
        }

        if self.path not in ("/", "/health"):
            self.send_response(404)
            payload["status"] = "not_found"
        else:
            self.send_response(200)

        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.end_headers()
        self.wfile.write(json.dumps(payload).encode("utf-8"))

    def log_message(self, format, *args):
        print(f"{SERVICE_NAME}: {format % args}")


if __name__ == "__main__":
    server = ThreadingHTTPServer(("0.0.0.0", PORT), StubHandler)
    print(f"{SERVICE_NAME} stub is running on port {PORT}")
    server.serve_forever()
