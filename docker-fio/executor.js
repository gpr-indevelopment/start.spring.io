const { execSync } = require('child_process');
const fs = require('fs');


exports.handler = async function(event) {
  const promise = new Promise(function(resolve, reject) {
        fs.unlink('/tmp/fio.dat', function(err) {
            if(err && err.code == 'ENOENT') {
                console.info("Fio file doesn't exist, won't remove it.");
            } else if (err) {
                console.error("Error occurred while trying to remove fio file");
            } else {
                console.info("Removed fio file");
            }
            let fioResult = execSync('fio --filename=/tmp/fio.dat --rw=write --direct=1 --bs=1M --ioengine=libaio --runtime=60 --numjobs=1 --time_based --group_reporting --name=seq_write --iodepth=1 --size=512M');
            resolve(fioResult);
        });
    })
  return promise
}