export const elements = {
    messagesContainer: document.getElementById('messages'),
    messageInput: document.getElementById('messageInput'),
    sendButton: document.getElementById('sendButton'),
    lobbyOverlay: document.getElementById('lobbyOverlay'),
    usernameInput: document.getElementById('usernameInput'),
    joinGameBtn: document.getElementById('joinGameBtn')
};

export function appendMessage(text, type, senderName, myUsername) {
    const messageEl = document.createElement('div');
    messageEl.classList.add('message');
    
    if (type === 'chat') {
        const isMe = senderName === myUsername;
        messageEl.classList.add(isMe ? 'sent' : 'received');
        messageEl.innerHTML = `<strong>${senderName}:</strong> ${text}`;
    } else {
        messageEl.classList.add('system');
        messageEl.textContent = text;
    }
    elements.messagesContainer.appendChild(messageEl);
    elements.messagesContainer.scrollTop = elements.messagesContainer.scrollHeight;
}
