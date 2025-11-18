import 'package:firebase_auth/firebase_auth.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:flutter_facebook_auth/flutter_facebook_auth.dart';

class AuthService {
  AuthService._internal();
  static final AuthService _instance = AuthService._internal();
  factory AuthService() => _instance;

  final FirebaseAuth _auth = FirebaseAuth.instance;
  final GoogleSignIn _googleSignIn = GoogleSignIn();

  Stream<User?> get authStateChanges => _auth.authStateChanges();

  // 1. REGISTRÁCIA (Správny názov metódy, obsahuje Email Verification)
  Future<User?> registerWithEmailAndPassword(
      String email, String password) async {
    final credential = await _auth.createUserWithEmailAndPassword(
      email: email,
      password: password,
    );
    await sendEmailVerification();
    return credential.user;
  }

  // 2. PRIHLÁSENIE
  Future<void> signInWithEmailAndPassword(String email, String password) async {
    await _auth.signInWithEmailAndPassword(email: email, password: password);
  }

  // 3. RESET HESLA
  Future<void> sendPasswordResetEmail(String email) async {
    await _auth.sendPasswordResetEmail(email: email);
  }

  // 4. OVERENIE E-MAILU
  Future<void> sendEmailVerification() async {
    final user = _auth.currentUser;
    if (user != null && !user.emailVerified) {
      await user.sendEmailVerification();
    }
  }

  // 5. SOCIÁLNE PRIHLÁSENIE (Google)
  Future<UserCredential?> signInWithGoogle() async {
    try {
      final GoogleSignInAccount? googleUser = await _googleSignIn.signIn();
      if (googleUser == null) return null;

      final googleAuth = await googleUser.authentication;

      final credential = GoogleAuthProvider.credential(
        accessToken: googleAuth.accessToken,
        idToken: googleAuth.idToken,
      );

      return await _auth.signInWithCredential(credential);
    } on FirebaseAuthException catch (e) {
      throw Exception(e.message ?? "Google Sign-In zlyhal vo Firebase.");
    } catch (e) {
      throw Exception(e.toString());
    }
  }

    // Ak používate Google, musíte odhlásiť aj Google klienta
    // await GoogleSignIn().signOut();
    // Ak používate Facebook, musíte odhlásiť aj Facebook klienta
    // await FacebookAuth.instance.logOut();


  // 🚀 FACEBOOK LOGIN – verzia pre flutter_facebook_auth: ^6.0.4
  Future<UserCredential?> signInWithFacebook() async {
   /* try {
      // 1. Otvorí natívne FB okno
      final LoginResult result = await FacebookAuth.instance.login();

      if (result.status == LoginStatus.success) {
        // 2. Máme token
        final accessToken = result.accessToken!;
        final credential = FacebookAuthProvider.credential(accessToken.token);

        // 3. Prihlásenie do Firebase
        return await _auth.signInWithCredential(credential);
      } else if (result.status == LoginStatus.cancelled) {
        // user klikol späť
        return null;
      } else {
        throw Exception(result.message ?? "Facebook Sign-In zlyhal.");
      }
    } on FirebaseAuthException catch (e) {
      throw Exception(e.message ?? "Facebook Sign-In zlyhal vo Firebase.");
    } catch (e) {
      throw Exception(e.toString());
    }*/
  }

  // 🚀 NOVÁ METÓDA: Prihlásenie cez APPLE 🚀
 /* Future<UserCredential?> signInWithApple() async {
    // Apple Sign-In je podporovaný len na iOS, macOS a webe
    if (!Platform.isIOS && !Platform.isMacOS) {
      throw UnsupportedError("Apple Sign-In je podporovaný len na zariadeniach Apple.");
    }

    try {
      // 1. Získanie natívnych Apple údajov
      final AuthorizationCredentialAppleID credential = await SignInWithApple.get
        (
        requests: [
          AppleIdRequest(
            requestedScopes: [
              AppleIdScope.email,
              AppleIdScope.fullName,
            ],
          ),
        ],
      );

      // 2. Vytvorenie Firebase Credential
      final OAuthCredential firebaseCredential = AppleAuthProvider.credential(
        idToken: credential.identityToken,
        rawNonce: credential.nonce,
      );

      // 3. Prihlásenie do Firebase
      return await _auth.signInWithCredential(firebaseCredential);

    } on FirebaseAuthException catch (e) {
      throw Exception(e.message ?? "Apple Sign-In zlyhal vo Firebase.");
    } on UnsupportedError catch (e) {
      // Znovu vyhodíme chybu, ak to nie je zariadenie Apple
      throw e;
    } catch (e) {
      throw Exception(e.toString());
    }
  }*/



  Future<void> logout() async {
    await _auth.signOut();

    // Odhlásenie aj z Google klienta (ak bol použitý)
    await _googleSignIn.signOut();

    // Odhlásenie aj z Facebook klienta (odkomentuj, ak budeš používať)
    // await FacebookAuth.instance.logOut();
  }

  User? get currentUser => _auth.currentUser;
}
