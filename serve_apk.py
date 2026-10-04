import http.server
import socketserver
import os
import sys

DIRECTORY = "/app/applet/public_download"
PORT = 3000

class APKHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def guess_type(self, path):
        if str(path).endswith(".apk"):
            return "application/vnd.android.package-archive"
        return super().guess_type(path)

    def end_headers(self):
        if self.path.endswith(".apk"):
            filename = os.path.basename(self.path.split("?")[0])
            self.send_header("Content-Disposition", f'attachment; filename="{filename}"')
            self.send_header("Cache-Control", "no-cache, must-revalidate")
        super().end_headers()

if __name__ == "__main__":
    os.chdir(DIRECTORY)
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), APKHandler) as httpd:
        print(f"Serving APKs at http://localhost:{PORT} from {DIRECTORY}", flush=True)
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            pass
