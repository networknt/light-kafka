#!/usr/bin/env python3
"""Reproduce the frozen wire baseline from exact Jackson 2 source, without updating it."""

import argparse
import difflib
import os
from pathlib import Path
import subprocess


BASELINE = "7b25cfaf641ed46003bde59e9f8b6ec39f7bb24a"
ROOT = Path(__file__).resolve().parents[1]


def run(command, log):
    with log.open("w") as output:
        subprocess.run(command, cwd=ROOT, stdout=output, stderr=subprocess.STDOUT, check=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--maven-repo", type=Path, required=True,
                        help="Isolated Maven cache used for the migration build")
    parser.add_argument("--output-dir", type=Path, required=True,
                        help="New evidence directory outside this checkout")
    args = parser.parse_args()
    cache = args.maven_repo.resolve()
    folder = args.output_dir.resolve()
    if cache == Path.home() / ".m2/repository":
        parser.error("Use an isolated Maven repository")
    if folder == ROOT or ROOT in folder.parents:
        parser.error("Evidence must remain outside the checkout")
    folder.mkdir(parents=True, exist_ok=False)
    source_dir = folder / "sources"
    source_dir.mkdir()
    classes = folder / "classes"
    classes.mkdir()

    # Fail if the exact baseline object is absent. Never substitute a moving branch.
    subprocess.run(["git", "cat-file", "-e", f"{BASELINE}^{{commit}}"], cwd=ROOT, check=True)
    files = []
    for module, names in {
        "kafka-producer": ("SchemaRecordSerializer", "NoSchemaRecordSerializer", "RegisteredSchema"),
        "kafka-entity": ("ProduceRecord", "ProduceRequest", "AuditRecord", "EmbeddedFormat"),
    }.items():
        for name in names:
            relative = f"{module}/src/main/java/com/networknt/kafka/{module.removeprefix('kafka-')}/{name}.java"
            data = subprocess.check_output(["git", "show", f"{BASELINE}:{relative}"], cwd=ROOT)
            target = source_dir / f"{name}.java"
            target.write_bytes(data)
            files.append(str(target))

    classpath_file = folder / "classpath.txt"
    # Dependencies only: the extracted Jackson 2 classes take precedence over
    # the installed Jackson 3 entity dependency. No current producer code is loaded.
    run(["mvn", "-o", f"-Dmaven.repo.local={cache}", "-pl", "kafka-producer",
         "dependency:build-classpath", f"-Dmdep.outputFile={classpath_file}"], folder / "dependencies.log")
    classpath = classpath_file.read_text().strip()
    generator = ROOT / "kafka-producer/src/test/baseline/Jackson2WireBaseline.java"
    run(["javac", "-cp", classpath, "-d", str(classes), *files, str(generator), str(ROOT / "kafka-producer/src/test/java/com/networknt/kafka/producer/WireCorpus.java"),
         str(ROOT / "kafka-producer/src/test/java/com/networknt/kafka/producer/WireScenario.java")], folder / "compile.log")
    runtime = str(classes) + os.pathsep + classpath
    run(["java", "-cp", runtime, "com.networknt.kafka.producer.Jackson2WireBaseline"], folder / "run.log")
    actual = "\n".join(line for line in (folder / "run.log").read_text().splitlines()
                       if line.startswith("WIRE|")) + "\n"
    (folder / "wire.txt").write_text(actual)
    (folder / "baseline-commit.txt").write_text(BASELINE + "\n")
    expected = (ROOT / "kafka-producer/src/test/resources/jackson2-baseline/wire.txt").read_text()
    if actual != expected:
        print("".join(difflib.unified_diff(expected.splitlines(True), actual.splitlines(True),
                                         fromfile="frozen", tofile="regenerated")))
        raise SystemExit("Jackson 2 wire baseline mismatch; frozen data was not changed")
    print(f"Matched all {len(actual.splitlines())} wire records from {BASELINE}")


if __name__ == "__main__":
    main()
