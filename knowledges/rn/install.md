# React Native 安装

## 安装环境依赖

### ruby & pod

```
brew install ruby@3.3
export PATH=/opt/homebrew/opt/ruby@3.3/bin:/opt/homebrew/lib/ruby/gems/3.3.0/bin:$PATH
gem install cocoapods
```

### Java

```
brew install --cask zulu@17
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home
```

## Create project with expo

使用 expo 安装

```
npx create-expo-app@latest
```

使用本地模拟器开发（iOS）

```
https://docs.expo.dev/get-started/set-up-your-environment/?platform=ios&device=simulated&mode=development-build&buildEnv=local
```

使用本地模拟器开发（Android）

```
https://docs.expo.dev/get-started/set-up-your-environment/?platform=android&device=simulated&mode=development-build&buildEnv=local
```
