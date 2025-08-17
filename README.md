开播提醒的云端数据部分是存在金山云文档
利用金山云文档的AirScript脚本功能实现数据的同步
以下是AirScript云端脚本内容： 
const sheet = Application.Sheets("Blued直播检测数据")
if(Context.argv.type === 'getAllData'){
  return getAllData()
}else if(Context.argv.type === 'addAnchor'){
  return addAnchor(Context.argv.data)
}else if(Context.argv.type === 'delAnchor'){
  return delAnchor(Context.argv.uid)
}
/**
* @param {string} uid
*/
function delAnchor(uid){
  let del = sheet.Range("D:D").Find(uid, undefined, xlFormulas)
  if (del == ""){
    return "删除失败，用户不存在！"
  };
  let name = sheet.Cells(del.Row, 1).Value2
  sheet.Rows(del.Row).Delete()
  return name + " 删除成功"
}
/**
* @param {undefined} [data]
*/
function addAnchor(data){
  let hasUser = sheet.Range("D:D").Find(data.uid, undefined, xlFormulas)
  if(hasUser){
    return "云端用户【" + data.name + '】已存在'
  }
  let totalRows = sheet.UsedRange.Rows.Count + 1
  sheet.Range("A" + totalRows).Value2 = data.name
  sheet.Range("B" + totalRows).Value2 = data.avatar
  sheet.Range("C" + totalRows).Value2 = data.live_id
  sheet.Range("D" + totalRows).Value2 = data.uid
  sheet.Range("E" + totalRows).Value2 = data.enc_uid
  sheet.Range("F" + totalRows).Value2 = data.is_voice_reminder
  sheet.Range("G" + totalRows).Value2 = data.is_force_reminder
  sheet.Range("H" + totalRows).Value2 = data.is_live_join
  sheet.Range("I" + totalRows).Value2 = data.is_download_avatar
  sheet.Range("J" + totalRows).Value2 = data.union_uid;
  return data.name + ' 添加成功'
}
function getAllData(){
  let totalRows = sheet.UsedRange.Rows.Count
  let data = []
  for(let i = 2;i <= totalRows;i++)
  {
    let name = sheet.Cells(i, 1)
    let avatar = sheet.Cells(i, 2)
    let live = sheet.Cells(i, 3)
    let uid = sheet.Cells(i, 4)
    let enc_uid = sheet.Cells(i, 5)
    let is_voice_reminder = sheet.Cells(i, 6)
    let is_force_reminder = sheet.Cells(i, 7)
    let is_live_join = sheet.Cells(i, 8)
    let is_download_avatar = sheet.Cells(i, 9)
    let union_uid = sheet.Cells(i, 10)
    data.push(
      {
        name: name.Text,
        avatar: avatar.Text,
        live: live.Text,
        uid: uid.Text,
        union_uid: union_uid.Text,
        enc_uid: enc_uid.Text,
        is_voice_reminder: is_voice_reminder.Text,
        is_force_reminder: is_force_reminder.Text,
        is_live_join: is_live_join.Text,
        is_download_avatar: is_download_avatar.Text
      }
    )
  }
  let jsonData = {
    code: 200,
    data: data
  }
  let jsonString = JSON.stringify(jsonData)
  console.log(jsonString)
  return jsonString
}
秘钥和接口请从金山云文档获取，并填入开播提醒功能页中进行保存。注：[版本：v1.2.3(102030)]开始支持此功能
版本：1.2.2（102020）以及之前是写死在代码中的，因仓库代码被其他人拉走导致云端数据混乱
相关秘钥已经删除，如果需要使用云端功能请升级版本或者自己编译一份。
