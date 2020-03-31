var fs = require("fs").promises;
const { csv_path } = require("./config");
let recordsFromCSV = [];

module.exports = async () => {
  let data = await fs.readFile(csv_path, "utf8");

  lines = data.split("\n");
  lines = lines.map(line => line.split(","));
  lines.map(line => {
    recordsFromCSV.push({
      tenantId: line[0],
      name: line[1],
      mobileNumber: line[2],
      correspondenceAddress: line[3],
      fatherOrHusbandName: line[4],
      dob: line[5],
      gender: line[6],
      roles: line[7],
      assignmentFromDate: line[8],
      department: line[9],
      designation: line[10],
      boundaryType: line[11],
      boundary: line[12],
      employeeType: line[13],
      employeeStatus: line[14]
    });
  });

  return await recordsFromCSV;
};
