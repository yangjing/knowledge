package yangbajing.utils.s.security

import org.bouncycastle.crypto.PBEParametersGenerator
import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.prng.DigestRandomGenerator
import org.bouncycastle.util.encoders.Base64

object PasswordUtil {
  private val GENERATOR: DigestRandomGenerator = new DigestRandomGenerator(new SHA512Digest)
  private val KEY_LENGTH: Int = 256
  private val ITERATIONS: Int = 5120

  def hash(plainPassword: String): String = {
    hash(plainPassword, salt(32), KEY_LENGTH, ITERATIONS)
  }

  def hash(plainPassword: String, salt: Array[Byte]): String = {
    hash(plainPassword, salt, KEY_LENGTH, ITERATIONS)
  }

  private def hash(plainPassword: String, salt: Array[Byte], keyLength: Int, iterations: Int): String = {
    val generator: PKCS5S2ParametersGenerator = new PKCS5S2ParametersGenerator
    generator.init(PBEParametersGenerator.PKCS5PasswordToBytes(plainPassword.toCharArray), salt, iterations)

    String.format("%s|%s", encode(salt), encode(generator.generateDerivedParameters(keyLength).asInstanceOf[KeyParameter].getKey))
  }

  def verify(plainPassword: String, _hash: String): Boolean = {
    hash(plainPassword, decode(extractSalt(_hash))) == _hash
  }

  private def salt(count: Int): Array[Byte] = {
    GENERATOR.addSeedMaterial((Math.random * 100000000).toLong)
    val salt: Array[Byte] = new Array[Byte](count)
    GENERATOR.nextBytes(salt)
    salt
  }

  private def encode(input: Array[Byte]): String = {
    Base64.toBase64String(input)
  }

  private def decode(input: String): Array[Byte] = {
    Base64.decode(input)
  }

  private def extractSalt(input: String): String = {
    input.substring(0, input.indexOf("|"))
  }
}
