package controllers

import javax.inject.Inject

import play.api.mvc.Action
import share.web.business.component.SettingComponent
import share.web.utils.BaseController

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-16.
  */
class PageController @Inject()(settingComponent: SettingComponent) extends BaseController {
  def index = Action {
    Ok(views.html.index("share2016", settingComponent.vendor.baidu.lbsyun.ak))
  }
}
