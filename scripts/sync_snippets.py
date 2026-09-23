#!/usr/bin/env python3
"""Copies code regions from this repo into the code blocks of markdown posts.

Mark a region in any Kotlin source:

    // region lesson:cube-buffers
    ...
    // endregion

and mark the code block in a post with a markdown comment on the line
before it (it renders as nothing):

    [//]: # (snippet cube-buffers)
    ```kotlin
    ...replaced on every sync...
    ```

The block is replaced with the region, dedented, with any nested region
markers dropped. With --check nothing is written, and the exit status is 1
if any post is out of date, so a build can fail on a stale snippet.
"""

import argparse
import pathlib
import re
import sys
import textwrap

REPO = pathlib.Path(__file__).resolve().parent.parent
DEFAULT_POSTS = REPO.parent / "gunnarss/site/src/jsMain/resources/markdown/blog"

REGION_START = re.compile(r"^\s*// region lesson:([\w-]+)\s*$")
REGION_END = re.compile(r"^\s*// endregion\b")
ANY_MARKER = re.compile(r"^\s*// (region|endregion)\b")
SNIPPET = re.compile(r"^\[//\]: # \(snippet ([\w-]+)\)\s*$")


def collect_regions():
    regions = {}
    for path in sorted(REPO.rglob("*.kt")):
        if any(part in ("build", ".gradle", ".kotlin") for part in path.parts):
            continue
        open_regions = []  # (name, first line) stack, for nesting
        lines = path.read_text().splitlines()
        for number, line in enumerate(lines):
            start = REGION_START.match(line)
            if start:
                open_regions.append((start.group(1), number + 1))
            elif REGION_END.match(line) and open_regions:
                name, first = open_regions.pop()
                if name in regions:
                    sys.exit(f"{path}: region lesson:{name} is also defined in {regions[name][0]}")
                body = [l for l in lines[first:number] if not ANY_MARKER.match(l)]
                regions[name] = (path, textwrap.dedent("\n".join(body)).strip("\n"))
        if open_regions:
            sys.exit(f"{path}: region lesson:{open_regions[-1][0]} has no // endregion")
    return regions


def sync(post, regions):
    lines = post.read_text().splitlines()
    out = []
    i = 0
    while i < len(lines):
        out.append(lines[i])
        marker = SNIPPET.match(lines[i])
        i += 1
        if not marker:
            continue
        name = marker.group(1)
        if name not in regions:
            sys.exit(f"{post}: no region lesson:{name} in {REPO}")
        if i >= len(lines) or not lines[i].startswith("```"):
            sys.exit(f"{post}: snippet {name} must be followed by a ``` code block")
        fence = lines[i]
        end = i + 1
        while end < len(lines) and lines[end] != "```":
            end += 1
        if end == len(lines):
            sys.exit(f"{post}: code block for snippet {name} is not closed")
        out.append(fence)
        out.extend(regions[name][1].splitlines())
        out.append("```")
        i = end + 1
    return "\n".join(out) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("posts", nargs="*", type=pathlib.Path,
                        help=f"markdown files (default: every .md in {DEFAULT_POSTS})")
    parser.add_argument("--check", action="store_true", help="report stale posts, write nothing")
    args = parser.parse_args()

    posts = args.posts or sorted(DEFAULT_POSTS.glob("*.md"))
    regions = collect_regions()
    stale = []
    for post in posts:
        before = post.read_text()
        after = sync(post, regions)
        if after != before:
            stale.append(post)
            if not args.check:
                post.write_text(after)
    for post in stale:
        print(f"{'stale' if args.check else 'updated'}: {post}")
    return 1 if args.check and stale else 0


if __name__ == "__main__":
    sys.exit(main())
