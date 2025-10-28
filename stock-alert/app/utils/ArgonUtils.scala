package utils

import de.mkammerer.argon2.Argon2Factory

object ArgonUtils {
  // Create a single Argon2 instance for hashing and verification
  private val argon2 = Argon2Factory.create()

  // Hashes the given address using Argon2
  // Parameters: iterations = 2, memory = 65536 KB, parallelism = 1
  def hashAddress(address: String): String = {
    argon2.hash(2, 65536, 1, address.toCharArray)
  }

  // Verifies whether a given address matches the stored Argon2 hash
  def verifyAddress(hash: String, address: String): Boolean = {
    argon2.verify(hash, address.toCharArray)
  }
}
