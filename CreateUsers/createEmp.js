const axios = require("axios");
const { base_url } = require("./config");

const exceldataToReqData = (exceldata, authToken) => {
  //exceldata.tenantId
  let roles = [];
  exceldata.roles.split("|").map(role => {
    roles.push({
      code: role,
      name: role.replace("_", " ")
    });
  });

  let assignmentFromDate = new Date(
    exceldata.assignmentFromDate.split("/")[2],
    exceldata.assignmentFromDate.split("/")[1] - 1,
    exceldata.assignmentFromDate.split("/")[0]
  ).getTime();

  let dob = new Date(
    exceldata.dob.split("/")[2],
    exceldata.dob.split("/")[1] - 1,
    exceldata.dob.split("/")[0]
  ).getTime();

  let emp = {
    RequestInfo: {
      authToken: authToken
    },
    Employees: [
      {
        tenantId: exceldata.tenantId,
        employeeStatus: exceldata.employeeStatus,
        user: {
          name: exceldata.name,
          mobileNumber: exceldata.mobileNumber,
          correspondenceAddress: exceldata.correspondenceAddress,
          fatherOrHusbandName: exceldata.fatherOrHusbandName,
          dob: dob,
          gender: exceldata.gender,
          roles: roles,
          tenantId: exceldata.tenantId
        },
        employeeType: exceldata.employeeType,
        jurisdictions: [
          {
            hierarchy: "REVENUE",
            boundaryType: exceldata.boundaryType,
            boundary: exceldata.boundary,
            tenantId: exceldata.tenantId
          }
        ],
        assignments: [
          {
            fromDate: assignmentFromDate,
            department: exceldata.department,
            designation: exceldata.designation,
            isCurrentAssignment: true,
            toDate: null
          }
        ],
        serviceHistory: [],
        education: [],
        tests: []
      }
    ]
  };

  console.log(JSON.stringify(emp));

  return emp;
};

module.exports = async (exceldata, authToken) => {
  try {
    let res = {};
    res = await axios({
      url: `${base_url}/egov-hrms/employees/_create?tenantId=${exceldata.tenantId}`,
      withCredentials: "include",
      headers: {
        Accept: "application/json, text/plain, */*",
        "Content-Type": "application/json;charset=UTF-8"
      },
      data: JSON.stringify(exceldataToReqData(exceldata, authToken)),
      method: "POST"
    });
  } catch (e) {
    console.error(e.response.data);
  }
  //console.log(data.data);
};
