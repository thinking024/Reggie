var web_prefix = '/backend'

const deleteImg = (img) => {
    return $axios({
        url: `/common`,
        method: 'delete',
        params: { name: img }
    })
}