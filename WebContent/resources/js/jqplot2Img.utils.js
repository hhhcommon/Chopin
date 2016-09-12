/**
 * 把jqplot变成img，参数为dom对象
 */
function jqplotToImg(obj) {
  var newCanvas = document.createElement("canvas");
  newCanvas.width = obj.find("canvas.jqplot-base-canvas").width()+20;
  newCanvas.height = obj.find("canvas.jqplot-base-canvas").height()+10;
  var baseOffset = "";
  baseOffset = obj.find("canvas.jqplot-base-canvas").offset();
  // make white background for pasting
  var context = newCanvas.getContext("2d");
  context.fillStyle = "rgba(255,255,255,1)";
  context.fillRect(0, 0, newCanvas.width, newCanvas.height);
  obj.children().each(function () {
    // for the div's with the X and Y axis
    if ($(this)[0].tagName.toLowerCase() == 'div') {
      // X axis is built with canvas
      $(this).children("canvas").each(function() {
        var offset = $(this).offset();
        newCanvas.getContext("2d").drawImage(this,
          offset.left - baseOffset.left+20,
          offset.top - baseOffset.top
        );
      });
      // Y axis got div inside, so we get the text and draw it on the canvas
      $(this).children("div").each(function() {
        var offset = $(this).offset();
        var context = newCanvas.getContext("2d");
        context.font = $(this).css('font-style') + " " + $(this).css('font-size') + " " + $(this).css('font-family');
        context.fillStyle = $(this).css('color');
        context.fillText($(this).text(),
          offset.left - baseOffset.left+20,
          offset.top - baseOffset.top + $(this).height()
        );
      });
    } else if($(this)[0].tagName.toLowerCase() == 'canvas') {
      // all other canvas from the chart
      var offset = $(this).offset();
      newCanvas.getContext("2d").drawImage(this,
        offset.left - baseOffset.left+20,
        offset.top - baseOffset.top
      );
    }
  });
  // add the point labels
  obj.children(".jqplot-point-label").each(function() {
    var offset = $(this).offset();
    var context = newCanvas.getContext("2d");
    context.font = $(this).css('font-style') + " " + $(this).css('font-size') + " " + $(this).css('font-family');
    context.fillStyle = $(this).css('color');
    context.fillText($(this).text(),
      offset.left - baseOffset.left+20,
      offset.top - baseOffset.top + $(this).height()*3/4
    );
  });
  //add the data labels
  obj.children(".jqplot-data-label").each(function() {
    var offset = $(this).offset();
    var context = newCanvas.getContext("2d");
    context.font = $(this).css('font-style') + " " + $(this).css('font-size') + " " + $(this).css('font-family');
    context.fillStyle = $(this).css('color');
    context.fillText($(this).text(),
      offset.left - baseOffset.left+20,
      offset.top - baseOffset.top + $(this).height()*3/4
    );
  });
  // add the title
  obj.children("div.jqplot-title").each(function() {
    var offset = $(this).offset();
    var context = newCanvas.getContext("2d");
    context.font = $(this).css('font-style') + " " + $(this).css('font-size') + " " + $(this).css('font-family');
    context.textAlign = $(this).css('text-align');
    context.fillStyle = $(this).css('color');
    context.fillText($(this).text(),
      newCanvas.width / 2,
      offset.top - baseOffset.top + $(this).height()
    );
  });
  // add the legend
  obj.children("table.jqplot-table-legend").each(function() {
    var offset = $(this).offset();
    var context = newCanvas.getContext("2d");
    context.strokeStyle = $(this).css('border-top-color');
    context.strokeRect(
      offset.left - baseOffset.left+20,
      offset.top - baseOffset.top,
      $(this).width(),$(this).height()
    );
    context.fillStyle = $(this).css('background-color');
    context.fillRect(
      offset.left - baseOffset.left+20,
      offset.top - baseOffset.top,
      $(this).width(),$(this).height()
    );
  });
  // add the rectangles
  obj.find("div.jqplot-table-legend-swatch").each(function() {
    var offset = $(this).offset();
    var context = newCanvas.getContext("2d");
    context.fillStyle = $(this).css('background-color');
    context.fillRect(
      offset.left - baseOffset.left+20,
      offset.top - baseOffset.top,
      $(this).parent().width(),$(this).parent().height()
    );
  });
  obj.find("td.jqplot-table-legend").each(function() {
    var offset = $(this).offset();
    var context = newCanvas.getContext("2d");
    context.font = $(this).css('font-style') + " " + $(this).css('font-size') + " " + $(this).css('font-family');
    context.fillStyle = $(this).css('color');
    context.textAlign = $(this).css('text-align');
    context.textBaseline = $(this).css('vertical-align');
    context.fillText($(this).text(),
      offset.left - baseOffset.left+20,
      offset.top - baseOffset.top + $(this).height()/2 + parseInt($(this).css('padding-top').replace('px',''))
    );
  });
  // convert the image to base64 format
  return newCanvas.toDataURL("image/png");
}