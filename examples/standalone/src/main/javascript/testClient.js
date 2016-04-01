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

  var payload = JSON.stringify([{
    "id": "5",
    "firstName": "Bob",
    "lastName": "Jingles",
    "birthYear": 1962,
    "socialSecurityNumber": 999999999
  }]);

  var headers = {"method": "addEmployee"};

  eventBus.send("/es/1.0", payload, headers, (error, result)=> {

    console.log("Vertx error result", error, result);

    /* Did vertx event bus return an error. */
    if (error) {
      console.log("error message from vertx or socketJS ", error);
    } else if (result.failureCode){
      console.log("error from app  ", result);
    }else{
        /*If successful show response from QBit. */
        console.log("Success ", JSON.parse(result.body))
    }
  });
}).catch((error) => {
  console.log(error)
});



connect('http://localhost:8080/eventbus/').then((eventBus) => {
  console.log("HERE.....")

  var headers = {"method": "list"};

  var payload = JSON.stringify([
    [{
      "id": "5",
      "firstName": "Bob",
      "lastName": "Jingles",
      "birthYear": 1962,
      "socialSecurityNumber": 999999999
    },
      {
        "id": "rick",
        "firstName": "Bob",
        "lastName": "Jingles",
        "birthYear": 1962,
        "socialSecurityNumber": 999999999
      }
    ]]);

  eventBus.send("/es/1.0", payload, headers, (error, result)=> {

    console.log("Vertx error result", error, result);

    /* Did vertx event bus return an error. */
    if (error) {
      console.log("error message from vertx or socketJS ", error);
    } else if (result.failureCode){
      console.log("error from app  ", result);
    }else{
      /*If successful show response from QBit. */
      console.log("Success ", JSON.parse(result.body))
    }
  });
}).catch((error) => {
  console.log(error)
});



connect('http://localhost:8080/eventbus/').then((eventBus) => {
  console.log("HERE.....")

  var headers = {"method": "twoArg"};

  var payload = JSON.stringify([
      {
        "id": "rick",
        "firstName": "Bob",
        "lastName": "Jingles",
        "birthYear": 1962,
        "socialSecurityNumber": 999999999
      },
      true
    ]);

  eventBus.send("/es/1.0", payload, headers, (error, result)=> {

    console.log("Vertx error result", error, result);

    /* Did vertx event bus return an error. */
    if (error) {
      console.log("error message from vertx or socketJS ", error);
    } else if (result.failureCode){
      console.log("error from app  ", result);
    }else{
      /*If successful show response from QBit. */
      console.log("Success ", JSON.parse(result.body))
    }
  });
}).catch((error) => {
  console.log(error)
});

setInterval(function () {
  console.log('Waiting...');
}, 3000);
