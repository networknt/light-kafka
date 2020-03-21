plugins {
    base
    java
    maven
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


allprojects {
    group = "com.networknt"
    version = "2.0.12-SNAPSHOT"
    repositories {
        mavenLocal() // mavenLocal must be added first.
        jcenter()
        maven(url="http://packages.confluent.io/maven")
    }
}

dependencies {
    // Make the root project archives configuration depend on every sub-project
    subprojects.forEach {
        archives(it)
    }
}

