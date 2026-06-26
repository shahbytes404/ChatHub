const state = {
    client: null,
    subscription: null,
    connected: false
}

const el = (id) => document.getElementById(id);

const wsUrlEl = el('wsUrl');
const tokenEl = el('token');
const conversationEl = el('conversationId');
const messageBodyEl = el('messageBody');
const logEl = el('log');
const statusBadgeEl = el('statusBadge');
const statusTextEl = el('statusText');

const connectBtn = el('connectBtn');
const subscribeBtn = el('subscribeBtn');
const disconnectBtn = el('disconnectBtn');
const clearBtn = el('clearBtn');
const sendMessageBtn = el('sendMessageBtn');
const typingStartBtn = el('typingStartBtn');
const typingStopBtn = el('typingStopBtn');
const heartbeatBtn = el('heartbeatBtn');

function now() {
    return new Date().toLocaleDateString();
}

function setStatus(connected) {
    state.connected = connected;
    statusBadgeEl.classList.toggle('ok', connected);
    statusTextEl.textContent = connected ? 'Connected' : 'Disconnected';
    connectBtn.disabled = connected;
    subscribeBtn.disabled = !connected;
    sendMessageBtn.disabled = !connected;
    disconnectBtn.disabled = !connected;
    typingStartBtn.disabled = !connected;
    typingStopBtn.disabled = !connected;
    heartbeatBtn.disabled = !connected;
}

function addLog(title, body, meta = []) {
    const item = document.createElement('div');
    item.className = 'log-item';

    const metaRow = document.createElement('div');
    metaRow.className = 'meta';
    metaRow.textContent = [now(), title, ...meta].filter(Boolean).join(' | ');

    const bodyNode = document.createElement('div');
    bodyNode.className = 'body';
    bodyNode.textContent = body;

    item.append(metaRow, bodyNode);
    logEl.prepend(item);
}

function prettyJson(value) {
    if (typeof value === 'string') {
        return JSON.stringify(JSON.parse(value), null, 2);
    }
    return JSON.stringify(value, null, 2);
}

function safePrettyJson(value) {
    try {
        return prettyJson(value);
    } catch (error) {
        return typeof value === 'string' ? value : String(value)
    }
}

function getToken() {
    return tokenEl.value.trim();
}

function makeClient() {
    const client = new StompJs.Client({
        brokerURL: wsUrlEl.value.trim(),
        connectHeaders: {
            Authorization: `Bearer ${getToken()}`
        },
        debug: (msg) => addLog('debug', msg),
        reconnectDelay: 0,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
    });

    client.onConnect = () => {
        addLog('socket', 'connected');
        setStatus(true);
    }

    client.onStompError = (frame) => {
        addLog('stomp-error', frame.body || '(empty body)', [frame.headers['message'] || '']);
    }

    client.onWebSocketClose = (event) => {
        addLog('socket', `closed code=${event.code} reason=${event.reason || ''}`);
        state.subscription = null;
        state.client = null;
        setStatus(false);
    }

    client.onWebSocketError = (event) => {
        addLog('socket-error', event?.message || 'websocket error');
    }

    return client;
}

function connect() {
    if (!getToken()) {
        alert('Paste a JWT token first');
        return;
    }
    if (state.client) {
        state.client.deactivate();
        state.client = null;
    }

    state.client = makeClient();
    addLog('connect', `connecting to ${wsUrlEl.value.trim()}`);
    state.client.activate();
}

function formatEventLabel(event) {
    const type = event?.type || 'EVENT';
    if (type === 'TYPING_STARTED') return 'Typing started';
    if (type === 'TYPING_STOPPED') return 'Typing stopped';
    if (type === 'MESSAGE_CREATED') return 'Message created';
    return type.replace(/_/g, ' ').toLowerCase();
}

async function sendMessage() {
    const conversationId = requireConversationId();
    const content = messageBodyEl.value.trim();
    if (!content) {
        throw new Error('Message content is required');
    }
    if (!state.client || !state.connected) {
        throw new Error('Connect first');
    }

    const response = await fetch(`/api/conversations/${conversationId}/messages`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`
        },
        body: JSON.stringify({clientMessageId: randomId(), content})
    })

    if (!response.ok) {
        throw new Error(await response.text() || `HTTP ${response.status}`);
    }

    await response.json();
    messageBodyEl.value = '';
}

function randomId() {
    if (crypto?.randomUUID()) {
        return crypto?.randomUUID();
    }
    return `msg_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}

function renderRealtimeEvent(event, raw) {

    const label = formatEventLabel(event);
    const payload = event?.payload;

    if (event?.type === 'MESSAGE_CREATED' && payload) {
        addLog('message received', payload.content || safePrettyJson(payload), [
            event.conversationId || '',
            event.actorUserId ? `sender=${event.actorUserId}` : '',
            event.messageId ? `message=${event.messageId}` : ''
        ]);
        return;
    }

    addLog(label, raw ? safePrettyJson(raw) : safePrettyJson(event), [
        event?.conversationId || '',
        event?.actorUserId ? `actor=${event.actorUserId}` : ''
    ]);
}

function subscribe() {
    if (!state.client || !state.connected) {
        return;
    }

    if (state.subscription) {
        state.subscription.unsubscribe();
        state.subscription = null;
    }

    state.subscription = state.client.subscribe('/user/queue/events', (message) => {
        const raw = message.body || '';
        try {
            const event = JSON.parse(raw);
            renderRealtimeEvent(event, raw);
        } catch (error) {
            addLog('event', raw);
        }
    })

    addLog('subscribe', 'subscribed to /user/queue/events', ['id=browser-events'])
}

function getConversationId() {
    return conversationEl.value.trim();
}

function requireConversationId() {
    const conversationId = getConversationId();
    if (!conversationId) {
        throw new Error('Conversation ID is required');
    }
    return conversationId;
}

function sendHeartbeat() {
    const conversationId = requireConversationId();
    if (!state.client || !state.connected) {
        throw new Error('Connect first');
    }

    state.client.publish({
            destination: '/app/presence/heartbeat',
            body: '{}',
            headers: {
                'content-type': 'application/json'
            }
        }
    )

    addLog('heartbeat', `sent for ${conversationId}`);
}

function sendTyping(typing) {
    const conversationId = requireConversationId();
    if (!state.client || !state.connected) {
        throw new Error('Connect first');
    }

    state.client.publish({
        destination: `/app/conversations/${conversationId}/typing`,
        body: JSON.stringify({typing}),
        headers: {
            'content-type': 'application/json'
        }
    })

    addLog('typing', JSON.stringify({typing}), [`/app/conversations/${conversationId}/typing`])
}

function disconnect() {
    if (state.subscription) {
        state.subscription.unsubscribe();
        state.subscription = null;
    }

    if (state.client) {
        state.client.deactivate();
        state.client = null;
    }

    setStatus(false);
    addLog('socket', 'disconnect request');
}

connectBtn.addEventListener('click', connect);
subscribeBtn.addEventListener('click', subscribe);
disconnectBtn.addEventListener('click', disconnect);
clearBtn.addEventListener('click', () => {
    logEl.innerHTML = '';
});
sendMessageBtn.addEventListener('click',
    () => sendMessage().catch((error) => addLog('error', error.message)))
typingStartBtn.addEventListener('click',
    () => sendTyping(true).catch((error) => addLog('error', error.message)));
typingStopBtn.addEventListener('click',
    () => sendTyping(false).catch((error) => addLog('error', error.message)));
heartbeatBtn.addEventListener('click',
    () => sendHeartbeat().catch((error) => addLog('error', error.message)));

setStatus(false);
addLog('ready', 'paste a JWT token and conversation ID, then connect to start');