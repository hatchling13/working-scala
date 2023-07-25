case class Region(nx: Int, ny: Int, name: String) {
  def forecastApiUrl(today: String, now: String): String = {
    s"${MyKeyUtil.apiUri}?serviceKey=${MyKeyUtil.key}&dataType=JSON&numOfRows=1000&pageNo=1" +
      s"&base_date=${today.replaceAll("-", "")}&base_time=$now&nx=$nx&ny=$ny"
  }
}
