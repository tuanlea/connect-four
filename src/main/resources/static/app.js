const messagesContainer = document.getElementById('messages');
const messageInput = document.getElementById('messageInput');
const sendButton = document.getElementById('sendButton');

// 1. Ask for a username before doing anything
let myUsername = prompt("Welcome! Please enter your username:") || "Anonymous";

// Create WebSocket connection dynamically based on the current host
const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
const socket = new WebSocket(`${protocol}//${window.location.host}/echo`);

function appendMessage(text, type, senderName) {
    const messageEl = document.createElement('div');
    messageEl.classList.add('message');
    
    if (type === 'chat') {
        // If the message is from me, style it as 'sent', otherwise 'received'
        const isMe = senderName === myUsername;
        messageEl.classList.add(isMe ? 'sent' : 'received');
        messageEl.innerHTML = `<strong>${senderName}:</strong> ${text}`;
    } else {
        // System messages (joined, left, connected)
        messageEl.classList.add('system');
        messageEl.textContent = text;
    }
    
    messagesContainer.appendChild(messageEl);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// Connection opened
socket.addEventListener('open', function (event) {
    messagesContainer.innerHTML = '';
    messageInput.disabled = false;
    sendButton.disabled = false;
    messageInput.focus();
    
    // 2. The Handshake: Tell the server who we are!
    const joinMessage = { type: 'join', username: myUsername };
    socket.send(JSON.stringify(joinMessage));
});

// Listen for messages
socket.addEventListener('message', function (event) {
    // 3. Parse the incoming JSON message
    const data = JSON.parse(event.data);
    
    if (data.type === 'system') {
        appendMessage(data.content, 'system');
    } else if (data.type === 'chat') {
        appendMessage(data.content, 'chat', data.username);
    }
});

// Listen for close
socket.addEventListener('close', function (event) {
    appendMessage('Disconnected from server.', 'system');
    messageInput.disabled = true;
    sendButton.disabled = true;
});

// Listen for error
socket.addEventListener('error', function (event) {
    appendMessage('Error connecting to server.', 'system');
});

function sendMessage() {
    const message = messageInput.value.trim();
    if (message) {
        // 4. Send chat message as JSON
        const chatPayload = { type: 'chat', content: message };
        socket.send(JSON.stringify(chatPayload));
        messageInput.value = '';
    }
}

sendButton.addEventListener('click', sendMessage);

messageInput.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
});
