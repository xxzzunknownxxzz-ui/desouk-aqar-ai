import http.server
import socketserver
import os
import sys
import time

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

def daemonize():
    try:
        pid = os.fork()
        if pid > 0:
            # Exit parent process
            sys.exit(0)
    except OSError as err:
        sys.stderr.write(f'fork failed: {err}\n')
        sys.exit(1)

    os.setsid()
    try:
        pid = os.fork()
        if pid > 0:
            sys.exit(0)
    except OSError as err:
        sys.stderr.write(f'fork 2 failed: {err}\n')
        sys.exit(1)

    # redirect standard file descriptors
    sys.stdout.flush()
    sys.stderr.flush()
    si = open('/dev/null', 'r')
    so = open('/tmp/apk_server.log', 'a+')
    se = open('/tmp/apk_server.log', 'a+')
    os.dup2(si.fileno(), sys.stdin.fileno())
    os.dup2(so.fileno(), sys.stdout.fileno())
    os.dup2(se.fileno(), sys.stderr.fileno())

def run():
    os.chdir(DIRECTORY)
    socketserver.TCPServer.allow_reuse_address = True
    while True:
        try:
            with socketserver.TCPServer(("", PORT), APKHandler) as httpd:
                print(f"[{time.ctime()}] Serving APKs at port {PORT} from {DIRECTORY}", flush=True)
                httpd.serve_forever()
        except Exception as e:
            print(f"Server error: {e}, retrying in 2s...", flush=True)
            time.sleep(2)

if __name__ == "__main__":
    daemonize()
    run()
