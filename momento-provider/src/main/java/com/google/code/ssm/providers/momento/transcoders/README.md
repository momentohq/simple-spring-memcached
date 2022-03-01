# Transcoders

Momento's SDK does not provide a `CacheTranscoder` implementation out of the box.
`CacheTranscoder` is a `memcached`-specific construct that is used to translate raw Java `Objects` into a byte array when sending data to and from a cache.

To support this library, we have included Couchbase's [own implementation of a CacheTranscoder from their spymemcached library](https://github.com/couchbase/spymemcached/tree/master/src/main/java/net/spy/memcached/transcoders).

This code is licensed under the MIT license and is structurally the same as Couchbase's code. The only changes that have been made are the removal of unnecessary boxing and unboxing of objects.

## Original license contents

```
Copyright (c) 2006-2009  Dustin Sallings
Copyright (c) 2009-2011  Couchbase, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
