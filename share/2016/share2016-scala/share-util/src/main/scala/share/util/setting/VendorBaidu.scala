package share.util.setting

import com.typesafe.config.Config

/**
  * Created by Yang Jing (yangbajing@gmail.com) on 2016-05-16.
  */
case class SettingLbsyun(ak: String)

case class VendorBaidu(lbsyun: SettingLbsyun)

object VendorBaidu {
  def apply(config: Config): VendorBaidu = {
    VendorBaidu(
      SettingLbsyun(config.getString("lbsyun.ak"))
    )
  }
}
