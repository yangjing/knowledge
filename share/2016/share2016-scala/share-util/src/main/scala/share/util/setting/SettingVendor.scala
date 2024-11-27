package share.util.setting

import com.typesafe.config.Config

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-16.
  */
case class SettingVendor(baidu: VendorBaidu)

object SettingVendor {
  def apply(config: Config): SettingVendor = {
    SettingVendor(VendorBaidu(config.getConfig("baidu")))
  }
}