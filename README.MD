Android AspectJ Gradle Plugin
===================================

[AspectJ](http://www.eclipse.org/aspectj/doc/released/devguide/bytecode-concepts.html) plugin for Android.

# SUPPORTS
* [Retrolambda](https://github.com/evant/gradle-retrolambda).
* [Multidex](https://developer.android.com/tools/building/multidex.html).
* [InstantRun](https://developer.android.com/studio/run/index.html#instant-run) (aspectj transformation is not incremental, see below).

# USAGE
## General usage
```
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        ...
        classpath 'com.github.vincentbrison:AndroidAspectJGradlePlugin:1.0.0'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'com.github.vincentbrison.android-aspectj-plugin'
```

## Extension
```
aspectj {
    include includeAspectPath = false; (true by default, see http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html)
}
```

## Logs
Full logs of aspectj weaving are located in :
```
{your-project}/{module}/{build}/ajc-details.log
```

## With [Retrolambda](https://github.com/evant/gradle-retrolambda):
```
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        ...
        classpath 'me.tatarka:gradle-retrolambda:3.0.1'
        classpath 'com.github.vincentbrison:AndroidAspectJGradlePlugin:1.0.0'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'me.tatarka.retrolambda'
apply plugin: 'com.github.vincentbrison.android-aspectj-plugin'
```

## About Instant Run
The aspectj task is not incremental, so even if you are using instant run, the full aspectj transformation will be applied.
Aspectj has an [incremental mode](http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html) but it is not supported by this plugin.
To bench the impact of this task on your build, you may use the following command :
```
gradle :{module}:compile{buildType}{flavor}AspectJ --profile
```

## Tests
Test repository is available [here](https://github.com/vincentbrison/AndroidAspectJGradlePlugin-Tests).

# DEVELOP BY
Fork and rewrite by [Vincent Brison](https://github.com/vincentbrison).
Originally developed by [Leo Lin](https://github.com/leolin310148).

# LICENSE
```
Copyright 2016 Vincent Brison, Leo Lin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
