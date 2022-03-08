function loginApi(data) {
    return $axios({
      'url': '/user/login',
      'method': 'post',
      data
    })
  }

function logoutApi() {
  return $axios({
    'url': '/user/logout',
    'method': 'post',
  })
}

function sendSmsApi(data) {
    return $axios({
        'url': '/user/sendsms',
        'method': 'post',
        data
    })
}
  