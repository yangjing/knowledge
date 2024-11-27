package share.web.business.component

import javax.inject.Inject

import play.api.Configuration
import share.util.setting.SettingVendor

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-16.
  */
class SettingComponent @Inject()(configuration: Configuration) {
  val vendor = SettingVendor(configuration.underlying.getConfig("share.vendor"))
}
