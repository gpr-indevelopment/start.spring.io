const https = require('https');

let usEastUrl = 'https://lkxqxjcgbykyrryqsqhf5rp6li0caxyg.lambda-url.us-east-1.on.aws/';
let saEastUrl = 'https://g4o3qkj4bnsnhjghrak4xijuuu0aqytg.lambda-url.sa-east-1.on.aws/';

let request = https.get(saEastUrl, (res) => {
    let data = '';

    res.on('data', (chunk) => {
      data += chunk;
    });

    res.on('close', () => {
      const regex = new RegExp("bw=.*\\/s");
      let responseJson = JSON.parse(data);
      let resultOutput = new TextDecoder().decode(new Uint8Array(responseJson.data));
      console.log("Full data " + resultOutput);
      console.log(regex.exec(resultOutput)[0]);
    });
  return;
});