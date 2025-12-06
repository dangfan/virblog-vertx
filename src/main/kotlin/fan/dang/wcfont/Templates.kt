package fan.dang.wcfont

object Templates {

    fun fontFaceCss(family: String, hash: String): String = """
@font-face {
    font-family: "$family";
    src: url(/wcfont/fonts/$hash) format("woff");
}
""".trimIndent()

    fun loaderJs(selector: String, family: String): String = """
var elems = document.querySelectorAll('$selector');
var text = '';
for (var i = 0; i != elems.length; ++i) {
  text += elems[i].textContent;
}
if (text) {
  var file = document.createElement('link');
  file.setAttribute('rel', 'stylesheet');
  file.setAttribute('type', 'text/css');
  file.setAttribute('href', '/wcfont/css?family=$family&content=' + encodeURIComponent(text));
  document.getElementsByTagName('head')[0].appendChild(file);
  for (i = 0; i != elems.length; ++i) {
    elems[i].style.cssText = 'font-family:"$family"';
  }
}
""".trimIndent()

}
