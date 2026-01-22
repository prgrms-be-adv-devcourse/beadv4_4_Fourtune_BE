// firebase-messaging-sw.js
importScripts("https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js");

const firebaseConfig = {
    apiKey: "AIzaSyCyGFtcYQrA4ebpoWZrcZzHrXlz2dZ6gK4",
    authDomain: "fourtune-9c05e.firebaseapp.com",
    projectId: "fourtune-9c05e",
    storageBucket: "fourtune-9c05e.firebasestorage.app",
    messagingSenderId: "927826319863",
    appId: "1:927826319863:web:1091feb08fff8fcf5036f2",
    measurementId: "G-YTZKL81JB3"
};

firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

// // 백그라운드에서 알림 수신 시 처리
// messaging.onBackgroundMessage((payload) => {
//     console.log('[firebase-messaging-sw.js] 백그라운드 메시지 수신:', payload);
//
//     // 알림 제목과 내용 추출
//     const notificationTitle = payload.notification.title;
//     const notificationOptions = {
//         body: payload.notification.body,
//         icon: '/firebase-logo.png' // 아이콘이 없으면 생략 가능
//     };
//
//     self.registration.showNotification(notificationTitle, notificationOptions);
// });
