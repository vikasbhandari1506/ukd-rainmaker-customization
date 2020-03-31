const getToken = require("./auth");
const getRecords = require("./read");
const createEmp = require("./createEmp");

const start = async () => {
  let failedRecords = [];
  const token = await getToken();
  const records = await getRecords();

  records.forEach((record, index) => {
    try {
      createEmp(record, token);
    } catch (e) {
      record.index = index;
      failedRecords.push(record);
    }
  });

  return failedRecords;
};

start().then(records => {
  if (records.length) {
    console.log(
      "========================= FAILED RECORDS ======================"
    );
    records.forEach(record =>
      console.log(record.index, " --> ", JSON.stringify(record))
    );
  }
});
