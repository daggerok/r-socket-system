const { RSocketClient, JsonSerializer, IdentitySerializer } = require('rsocket-core');
const RSocketWebSocketClient = require('rsocket-websocket-client').default;
let client = undefined;

document.querySelector('#app').innerHTML = `
  <h1>Hello Parcel!</h1>
  <a href="https://parceljs.org/" target="_blank">Documentation</a>
  <div id="r-socket">
    <li></li>
  </div>
`

document.addEventListener('DOMContentLoaded', main);

function main() {
  if (client !== undefined) {
    client.close();
    ul().innerHTML = '';
  }

  // Create an instance of a client
  client = new RSocketClient({
    serializers: {
      data: JsonSerializer,
      metadata: IdentitySerializer
    },
    setup: {
      // ms btw sending keepalive to server
      keepAlive: 60000,
      // ms timeout if no keepalive response
      lifetime: 180000,
      // format of `data`
      dataMimeType: 'application/json',
      // format of `metadata`
      metadataMimeType: 'message/x.rsocket.routing.v0',
    },
    transport: new RSocketWebSocketClient({
      url: 'ws://127.0.0.1:8002/r-socket'
    }),
  });

  // Open the connection
  client.connect().subscribe({
    onComplete: socket => {
      // socket provides the rsocket interactions fire/forget, request/response,
      // request/stream, etc as well as methods to close the socket.
      socket.requestStream({
        // data: {
        //   'ololo': 'trololo',
        // },
        metadata: String.fromCharCode('hello'.length) + 'hello',
      }).subscribe({
        onComplete: () => console.log('complete'),
        onError: error => {
          console.log(error);
          addListItem(ul(), 'Connection has been closed due to ' + error);
        },
        onNext: payload => {
          console.log(payload.data);
          reloadMessages(payload.data);
        },
        onSubscribe: subscription => {
          subscription.request(2147483647);
        },
      });
    },
    onError: error => {
      console.log(error);
      addListItem(ul(), 'Connection has been refused due to ', error);
    },
    onSubscribe: cancel => {
      /* call cancel() to abort */
    }
  });
}

function ul() {
  return document.getElementById('r-socket-app');
}

function reloadMessages(message) {
  console.log('message 1', message)
  const listItems = ul().getElementsByTagName('li');

  for (let i = 0; i < listItems.length; i++) {
    if (listItems[i].innerText.includes(message['id'])) return;
  }

  addListItem(JSON.stringify(message));
}

function addListItem(text) {
  const li = document.createElement('li');
  li.appendChild(document.createTextNode(text));
  ul().prepend(li);
}
