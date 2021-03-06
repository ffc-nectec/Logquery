/*
 * Copyright (c) 2018 NECTEC
 *   National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

importScripts('https://www.gstatic.com/firebasejs/4.13.0/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/4.13.0/firebase-messaging.js');
importScripts('jquery-3.3.1.min.js');

firebase.initializeApp({
    'messagingSenderId': '656545465'
});


const messaging = firebase.messaging();


messaging.setBackgroundMessageHandler(function (payload) {
    console.log('[firebase-messaging-sw.js] Received background message ', payload);
    // Customize notification here
    var notificationTitle = 'Background Message Title';
    var notificationOptions = {
        body: "FFC Sync ",
        icon: 'https://avatars0.githubusercontent.com/u/18624547?s=60&v=4'
    };

    var messageToAir = {"message": payload};

    $.ajax({
        type: 'post',
        url: '/event',
        data: JSON.stringify(messageToAir),
        contentType: "application/json",
        traditional: true,
        dataType: "application/json"
    });

    return self.registration.showNotification(notificationTitle,
        notificationOptions);
});
