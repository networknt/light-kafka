plugins {
    java
    maven
}

repositories {
    maven(url = "https://packages.confluent.io/maven/")
}

dependencies {

    val slf4jVersion: String by project
    compile("org.slf4j", "slf4j-api", slf4jVersion)

    val light4jVersion: String by project
    compile("com.networknt", "service", light4jVersion)
    compile("com.networknt", "config", light4jVersion)

    val avroVersion: String by project
    compile("org.apache.avro", "avro", avroVersion)

    val confluentVersion: String by project
    compile("io.confluent", "kafka-schema-registry-client", confluentVersion)

    val kafkaVersion: String by project
    compile("org.apache.kafka", "kafka-clients", kafkaVersion)

    val jacksonVersion: String by project
    compile("com.fasterxml.jackson.core", "jackson-databind", jacksonVersion)

    val junitVersion: String by project
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-params", junitVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)

    val logbackVersion: String by project
    testImplementation("ch.qos.logback", "logback-classic", logbackVersion)
}
