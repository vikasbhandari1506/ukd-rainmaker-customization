const getToken = require("./auth");
const getRecords = require("./read");
const exceldataToReqData = require("./createEmp");
const axios = require("axios");
const { base_url } = require("./config");

const start = async () => {
  let failedRecords = [];
  const token = await getToken();
  const records = await getRecords();

  for (let index = 0; index < records.length; index++) {
    const record = records[index];
    let req = exceldataToReqData(record, token);

    //console.log(req.Employees[0].user);

    await axios({
      url: `${base_url}/egov-hrms/employees/_create?tenantId=${record.tenantId}`,
      withCredentials: "include",
      headers: {
        Accept: "application/json, text/plain, */*",
        "Content-Type": "application/json;charset=UTF-8"
      },
      data: JSON.stringify(req),
      method: "POST"
    })
      .then(response => {
        console.log(
          "Creating user for id => ",
          req.Employees[0].user.name,
          ", ",
          req.Employees[0].user.mobileNumber,
          " with username ==> ",
          response.data.Employees[0].code
        );
      })
      .catch(error => {
        error.response ? console.log(error.response.data) : console.log(error);
        record.index = index + 1;
        failedRecords.push(record);
      });
  }

  return failedRecords;
};

start().then(records => {
  if (records.length) {
    console.log(
      "========================= FAILED RECORDS ======================"
    );
    records.forEach(record =>
      console.log(record.index, " --> ", JSON.stringify(Object.values(record)))
    );
  }
});
