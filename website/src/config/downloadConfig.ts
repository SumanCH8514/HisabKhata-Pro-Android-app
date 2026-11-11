export interface AppRelease {
  version: string
  buildNumber: number
  releaseDate: string
  apkSize: string
  apkSizeBytes: number
  minAndroid: string
  minSdk: number
  targetSdk: number
  architecture: string
  sha256: string
  downloadUrl: string
  releasesUrl: string
  repoUrl: string
  webAppUrl: string
  packageName: string
}

export const APP_CONFIG: AppRelease = {
  version: "1.1.0",
  buildNumber: 2,
  releaseDate: "November 2026",
  apkSize: "21.8 MB",
  apkSizeBytes: 22827750,
  minAndroid: "Android 8.0 (Oreo)+",
  minSdk: 26,
  targetSdk: 35,
  architecture: "Universal (arm64-v8a, armeabi-v7a, x86_64)",
  sha256: "0E28F1728A8A6D7EE068609A60A04160DB09F65199831D9E3A828BE78984896A",
  downloadUrl: "https://github.com/SumanCH8514/HisabKhata-Pro-Android-app/raw/main/apk/app-debug.apk",
  releasesUrl: "https://github.com/SumanCH8514/HisabKhata-Pro-Android-app/releases",
  repoUrl: "https://github.com/SumanCH8514/HisabKhata-Pro-Android-app",
  webAppUrl: "https://hisabkhata.sumanonline.com/",
  packageName: "com.sumanonline.hisabkhata"
}
