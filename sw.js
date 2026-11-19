// 앱 설치용 최소 서비스 워커: 항상 최신 파일을 인터넷에서 받아오고, 끊겼을 때만 저장본 사용
const CACHE = "english-talk-v8";
self.addEventListener("install", e => {
  self.skipWaiting();
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(["./", "./index.html", "./manifest.json", "./icon-192.png", "./icon-512.png"])));
});
self.addEventListener("activate", e => e.waitUntil(self.clients.claim()));
self.addEventListener("fetch", e => {
  const req = e.request;
  if (req.method !== "GET" || new URL(req.url).origin !== location.origin) return; // AI 호출 등은 건드리지 않음
  e.respondWith(
    fetch(req).then(res => { const copy = res.clone(); caches.open(CACHE).then(c => c.put(req, copy)); return res; })
      .catch(() => caches.match(req).then(r => r || caches.match("./index.html")))
  );
});
