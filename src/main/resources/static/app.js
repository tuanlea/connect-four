const messagesContainer = document.getElementById('messages');
const messageInput = document.getElementById('messageInput');
const sendButton = document.getElementById('sendButton');

// Create WebSocket connection dynamically based on the current host
const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
const socket = new WebSocket(`${protocol}//${window.location.host}/echo`);

function appendMessage(text, type) {
    const messageEl = document.createElement('div');
    messageEl.classList.add('message', type);
    messageEl.textContent = text;
    messagesContainer.appendChild(messageEl);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// Connection opened
socket.addEventListener('open', function (event) {
    messagesContainer.innerHTML = '';
    appendMessage('Connected to server.', 'system');
    messageInput.disabled = false;
    sendButton.disabled = false;
    messageInput.focus();
});

// Listen for messages
socket.addEventListener('message', function (event) {
    appendMessage(event.data, 'received');
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
        socket.send(message);
        appendMessage(message, 'sent');
        messageInput.value = '';
    }
}

sendButton.addEventListener('click', sendMessage);

messageInput.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
});
