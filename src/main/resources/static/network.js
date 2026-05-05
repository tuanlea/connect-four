import { appendMessage, elements } from './ui.js';
import { myUsername, myPlayer, otherPlayers } from './game.js';

let socket = null;

export function connectToServer() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    socket = new WebSocket(`${protocol}//${window.location.host}/echo`);

    socket.addEventListener('open', function () {
        elements.messagesContainer.innerHTML = '';
        elements.messageInput.disabled = false;
        elements.sendButton.disabled = false;
        elements.messageInput.focus();
        
        socket.send(JSON.stringify({ type: 'join', username: myUsername }));
        socket.send(JSON.stringify({ type: 'move', x: myPlayer.x, y: myPlayer.y }));
    });

    socket.addEventListener('message', function (event) {
        const data = JSON.parse(event.data);
        
        if (data.type === 'system') {
            appendMessage(data.content, 'system', null, myUsername);
            if (data.content.includes('has left') && data.username) {
                delete otherPlayers[data.username];
            }
        } 
        else if (data.type === 'chat') {
            appendMessage(data.content, 'chat', data.username, myUsername);
        }
        else if (data.type === 'move') {
            if (data.username !== myUsername) {
                otherPlayers[data.username] = { x: data.x, y: data.y };
            }
        }
    });

    socket.addEventListener('close', function () {
        appendMessage('Disconnected from server.', 'system', null, myUsername);
        elements.messageInput.disabled = true;
        elements.sendButton.disabled = true;
    });

    socket.addEventListener('error', function () {
        appendMessage('Error connecting to server.', 'system', null, myUsername);
    });
}

export function sendMove(x, y) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ type: 'move', x: x, y: y }));
    }
}

export function sendChat(message) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ type: 'chat', content: message }));
    }
}
