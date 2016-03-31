const EventBus = require("vertx3-eventbus-client");


function connect(url) {
  console.log(`Connecting to vertx: ${url}`);
  return new Promise((resolve, reject) => {
    try {
      const eventBus = new EventBus(url);
      eventBus.onopen = () => {
        console.log(`established connection to vertx on ${url}`);
        resolve(eventBus);
      };
      eventBus.onclose = () => {
        console.log(`connection closed on ${url}`);
      };
      eventBus.onerror = (error) => {
        console.log("there was an error: ", error);
        reject(error);
      }
    } catch (e) {
      console.log(`exception encountered connecting to ${url}`);
      reject(e);
    }
  });
}

connect('http://localhost:8080/eventbus/').then((eventBus) => {
  console.log("HERE.....")
  eventBus.send("testservice",
    JSON.stringify({"method": "test", "args": ["hello"]}), (error, result)=> {
      if (error) {
        console.log("error message ", error);
      } else {
        console.log("result message ", result);
      }
    });
}).catch((error) => {
  console.log(error)
});

setInterval(function () {
  console.log('Waiting...');
}, 3000);