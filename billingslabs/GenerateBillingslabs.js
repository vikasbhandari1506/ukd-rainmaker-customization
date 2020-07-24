const fs = require("fs");
const http = require("follow-redirects").http;

let wardWise = true;
let tenantid = "uk.dehradun";
let fromDate = "2018-04-01";
let toDate = "2019-03-31";
let createdBy = "1234";
let createdTime = "1594898987000";
let lastModifiedBy = "1234";
let lastModifiedTime = "1594898987000";
let wardRatePath =
  "/Users/sivanalluri/Downloads/Dehradun_Unit_Rates_Roorkee(2018-19).xls - Sheet1.csv";
let mohallaRatePath =
  "/Users/sivanalluri/Downloads/Dehradun_Unit_Rates_Roorkee(2018-19).xls - Sheet1.csv";
let locationFilePath =
  "/Users/sivanalluri/codebase/ukd-mdms-data/data/uk/dehradun/egov-location/boundary-data.json";

function getMohallas() {
  return new Promise((resolve, reject) => {
    fs.readFile(locationFilePath, "utf8", (err, jsonString) => {
      if (err) reject("File read failed:", err);
      boundary = JSON.parse(jsonString);
      let mohallas = [];

      boundary.TenantBoundary[0].boundary.children[0].children.forEach(
        (ele) => {
          ele.children.map((e) =>
            mohallas.push({
              mohalla: e.code,
              ward: ele.name.split("(")[0].trim(),
              wardcode: ele.code,
            })
          );
        }
      );
      resolve(mohallas);
    });
  });
}

function getWardRates() {
  return new Promise((resolve, reject) => {
    fs.readFile(wardRatePath, "utf8", (err, data) => {
      if (err) reject("File read failed:", err);

      let wards = {};
      data = data.split("\r\n");
      data.splice(0, 2);

      data.forEach((ele) => {
        let rates = ele.split(",");
        let wardName = rates.splice(0, 1);
        wards[wardName[0].split("(")[0].trim()] = rates;
      });
      resolve(wards);
    });
  });
}

function getMohallasRates() {
  return new Promise((resolve, reject) => {
    fs.readFile(mohallaRatePath, "utf8", (err, data) => {
      if (err) reject("File read failed:", err);

      let wards = {};
      data = data.split("\r\n");
      data.splice(0, 2);

      data.forEach((ele) => {
        let rates = ele.split(",");
        let wardName = rates.splice(0, 1);
        rates.splice(0, 1);
        wards[wardName[0].split("(")[0].trim()] = rates;
      });
      resolve(wards);
    });
  });
}

PushBS = (bSlabs) => {
  var options = {
    method: "POST",
    hostname: "localhost",
    port: 8080,
    path: "/pt-calculator-v2/billingslab/_create?tenantId=uk.dehradun",
    headers: {
      "Content-Type": "application/json",
      accept: "application/json, text/plain, */*",
      "accept-encoding": "gzip, deflate, br",
      "accept-language": "en-GB,en-US;q=0.9,en;q=0.8",
    },
  };

  var req = http.request(options, function (res) {
    var chunks = [];

    res.on("data", function (chunk) {
      chunks.push(chunk);
    });

    res.on("end", function (chunk) {
      var body = Buffer.concat(chunks);
      var body = JSON.parse(body.toString());
      if (
        !(
          body.billingSlab &&
          body.billingSlab.length &&
          body.billingSlab.length === 12
        )
      )
        console.log(
          "Failed pushing billing slabs for ward: " +
            bSlabs[0].ward +
            " mohalla: " +
            bSlabs[0].mohalla +
            " with response code  " +
            JSON.stringify(body)
        );
    });

    res.on("error", function (error) {
      console.error("Error while pushing data ", error);
    });
  });

  var postData = JSON.stringify({
    RequestInfo: {
      apiId: "Rainmaker",
      ver: ".01",
      ts: "",
      action: "_create",
      did: "1",
      key: "",
      msgId: "20170310130900|en_IN",
      authToken: "15795f56-b01d-4d7f-b265-469f79fb8686",
      userInfo: {
        id: 5,
        userName: "EMP-000003",
        salutation: null,
        name: "Dharma",
        gender: "MALE",
        mobileNumber: "9123456782",
        emailId: "asd@123.com",
        altContactNumber: null,
        pan: null,
        aadhaarNumber: null,
        permanentAddress: "dehradun",
        permanentCity: "dehradun",
        permanentPinCode: null,
        correspondenceAddress: "corres add",
        correspondenceCity: "dehradun",
        correspondencePinCode: null,
        addresses: [
          {
            pinCode: null,
            city: "dehradun",
            address: "corres add",
            type: "CORRESPONDENCE",
            id: 9,
            tenantId: "uk.dehradun",
            userId: 5,
            addressType: "CORRESPONDENCE",
            lastModifiedDate: null,
            lastModifiedBy: null,
          },
          {
            pinCode: null,
            city: "dehradun",
            address: "dehradun",
            type: "PERMANENT",
            id: 15,
            tenantId: "uk.dehradun",
            userId: 5,
            addressType: "PERMANENT",
            lastModifiedDate: null,
            lastModifiedBy: null,
          },
        ],
        active: true,
        locale: null,
        type: "EMPLOYEE",
        accountLocked: false,
        accountLockedDate: 0,
        fatherOrHusbandName: "K",
        signature: null,
        bloodGroup: null,
        photo: null,
        identificationMark: null,
        createdBy: 2,
        lastModifiedBy: 1,
        tenantId: "uk.dehradun",
        roles: [
          {
            code: "EMPLOYEE",
            name: "Employee",
            tenantId: "uk.dehradun",
          },
          {
            code: "TL_CEMP",
            name: "TL Counter Employee",
            tenantId: "uk.dehradun",
          },
          {
            code: "PTCEMP",
            name: "PT Counter Employee",
            tenantId: "uk.dehradun",
          },
        ],
        uuid: "aa883f8c-e094-4088-badb-ae1ae474d1ac",
        createdDate: "22-05-2019 15:45:39",
        lastModifiedDate: "18-07-2019 16:25:45",
        dob: "28/10/1991",
        pwdExpiryDate: "20-08-2019 15:45:39",
      },
    },
    BillingSlab: bSlabs,
  });
  //console.log(postData);

  req.write(postData);

  req.end();
};

Promise.all([
  getMohallas(),
  wardWise ? getWardRates() : getMohallasRates(),
]).then(([mohallas, rates]) => {
  mohallas.forEach((mohalla) => {
    let bSlabs = [];

    if (rates[mohalla.ward]) {
      bSlabs.push({
        propertyType: "BUILTUP",
        roadType: "RD1",
        constructionType: "PUCCA",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][0] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });
      bSlabs.push({
        propertyType: "BUILTUP",
        roadType: "RD2",
        constructionType: "PUCCA",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][1] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });
      bSlabs.push({
        propertyType: "BUILTUP",
        roadType: "RD3",
        constructionType: "PUCCA",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][2] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });

      bSlabs.push({
        propertyType: "BUILTUP",
        roadType: "RD1",
        constructionType: "SEMIPUCCA",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][3] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });
      bSlabs.push({
        propertyType: "BUILTUP",
        roadType: "RD2",
        constructionType: "SEMIPUCCA",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][4] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });
      bSlabs.push({
        propertyType: "BUILTUP",
        roadType: "RD3",
        constructionType: "SEMIPUCCA",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][5] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });

      bSlabs.push({
        propertyType: "BUILTUP",
        roadType: "RD1",
        constructionType: "KUCCHA",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][6] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });
      bSlabs.push({
        propertyType: "BUILTUP",
        roadType: "RD2",
        constructionType: "KUCCHA",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][7] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });
      bSlabs.push({
        propertyType: "BUILTUP",
        roadType: "RD3",
        constructionType: "KUCCHA",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][8] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });

      bSlabs.push({
        propertyType: "VACANT",
        roadType: "RD1",
        constructionType: "",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][9] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });
      bSlabs.push({
        propertyType: "VACANT",
        roadType: "RD2",
        constructionType: "",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][10] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });
      bSlabs.push({
        propertyType: "VACANT",
        roadType: "RD3",
        constructionType: "",
        ward: mohalla.wardcode,
        mohalla: mohalla.mohalla,
        unitRate: rates[mohalla.ward][11] || 0,
        fromDate: fromDate,
        toDate: toDate,
        auditDetails: {
          createdBy: createdBy,
          lastModifiedBy: lastModifiedBy,
          createdTime: createdTime,
          lastModifiedTime: lastModifiedTime,
        },
        tenantId: tenantid,
      });

      PushBS(bSlabs);
    } else {
      console.log("Didn't find the match for the WARD : ", mohalla.ward);
    }
  });

  console.log("Done. Total Mohalla count : ", mohallas.length);
});
