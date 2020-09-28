plugins {
    java
    maven
}

dependencies {
    compile(project(":kafka-common"))

    val slf4jVersion: String by project
    compile("org.slf4j", "slf4j-api", slf4jVersion)

    val light4jVersion: String by project
    compile("com.networknt", "service", light4jVersion)
    compile("com.networknt", "config", light4jVersion)

    val avroVersion: String by project
    compile("org.apache.avro", "avro", avroVersion)

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
