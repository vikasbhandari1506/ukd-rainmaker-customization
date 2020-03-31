const {
  admin_username,
  admin_password,
  admin_tenant,
  base_url
} = require("./config");
const axios = require("axios");

module.exports = async () => {
  let res = await axios({
    url: `${base_url}/user/oauth/token`,
    withCredentials: "include",
    headers: {
      Accept: "application/json, text/plain, */*",
      Authorization: "Basic ZWdvdi11c2VyLWNsaWVudDplZ292LXVzZXItc2VjcmV0",
      "Content-Type": "application/x-www-form-urlencoded"
    },
    data: `username=${admin_username}&password=${admin_password}&grant_type=password&scope=read&tenantId=${admin_tenant}&userType=EMPLOYEE`,
    method: "POST"
  });
  //console.log(data.data);

  return await res.data.access_token;
};
