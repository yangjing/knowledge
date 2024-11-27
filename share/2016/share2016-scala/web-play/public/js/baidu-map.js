/**
 * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-16.
 */
// 百度地图API功能
$(function () {
  var map = new BMap.Map("share-location");
  var poi = new BMap.Point(106.50541988318, 29.619915860857);
  map.centerAndZoom(poi, 18);
  map.addControl(new BMap.MapTypeControl());
  map.setCurrentCity("重庆");
  map.enableScrollWheelZoom(true);

  var content = '<div style="margin:0;line-height:20px;padding:2px;">' +
    '<img src="/assets/img/kylin-mountain-C.jpg" alt="" style="float:right;zoom:1;overflow:hidden;width:100px;height:100px;margin-left:3px;"/>' +
    '地址：重庆市北部新区麒麟座C栋9楼<br/>简介：麒麟座C栋位于重庆市渝北区光电园地铁站附近，为综合研发及办公楼。' +
    '</div>';

//创建检索信息窗口对象
  var searchInfoWindow = null;
  searchInfoWindow = new BMapLib.SearchInfoWindow(map, content, {
    title: "麒麟座C栋",      //标题
    width: 320,             //宽度
    height: 105,              //高度
    panel: "panel",         //检索结果面板
    enableAutoPan: true,     //自动平移
    searchTypes: [
      BMAPLIB_TAB_SEARCH,   //周边检索
      BMAPLIB_TAB_TO_HERE,  //到这里去
      BMAPLIB_TAB_FROM_HERE //从这里出发
    ]
  });
  var marker = new BMap.Marker(poi); //创建marker对象
  marker.enableDragging(); //marker可拖拽
  marker.addEventListener("click", function (e) {
    searchInfoWindow.open(marker);
  });
  map.addOverlay(marker); //在地图中添加marker
//样式1
  var searchInfoWindow1 = new BMapLib.SearchInfoWindow(map, "信息框1内容", {
    title: "信息框1", //标题
    panel: "panel", //检索结果面板
    enableAutoPan: true, //自动平移
    searchTypes: [
      BMAPLIB_TAB_FROM_HERE, //从这里出发
      BMAPLIB_TAB_SEARCH   //周边检索
    ]
  });

  function openInfoWindow1() {
    searchInfoWindow1.open(new BMap.Point(106.51541988318, 29.619905860857));
  }

//样式2
  var searchInfoWindow2 = new BMapLib.SearchInfoWindow(map, "信息框2内容", {
    title: "信息框2", //标题
    panel: "panel", //检索结果面板
    enableAutoPan: true, //自动平移
    searchTypes: [
      BMAPLIB_TAB_SEARCH   //周边检索
    ]
  });

  function openInfoWindow2() {
    searchInfoWindow2.open(new BMap.Point(106.52541988318, 29.619905860857));
  }

//样式3
  var searchInfoWindow3 = new BMapLib.SearchInfoWindow(map, "信息框3内容", {
    title: "信息框3", //标题
    width: 260, //宽度
    height: 40, //高度
    panel: "panel", //检索结果面板
    enableAutoPan: true, //自动平移
    searchTypes: []
  });

  function openInfoWindow3() {
    searchInfoWindow3.open(new BMap.Point(106.52541988318, 29.619905860857));
  }
});