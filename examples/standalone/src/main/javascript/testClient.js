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
  eventBus.send("/es/1.0",     JSON.stringify(
    { "method":"addEmployee",
      "args":
        [{"id":"5","firstName":"Bob","lastName":"Jingles","birthYear":1962,"socialSecurityNumber":999999999}]
    }), (error, result)=>{

    /* Did vertx event bus return an error. */
    if (error) {
      console.log("error message from vertx or socketJS ", error);
    } else {
      const response = JSON.parse (result.body);
      console.log("result message from Vertx", result);
      console.log("response message from QBit", response);

      /* Did QBit return an error. */
      if (response.error) {
        console.log("error cause message ", response['cause']);
      } else {
        /*If successful show response from QBit. */
        console.log("Success ", response['returned'])
      }
    }
  });
}).catch((error) => {
  console.log(error)
});

setInterval(function () {
  console.log('Waiting...');
}, 3000);
