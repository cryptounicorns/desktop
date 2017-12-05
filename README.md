### Development

```
boot watch dev-build
```

```
electron target/
```

```
boot repl -c
boot.user=> (start-repl)
```

### Lint and check

```
boot check-sources
```

### Format code

```
boot fmt --git --mode overwrite --really --options '{:style :community :map {:comma? false} :vector {:wrap? false}}'
```

### Packaging

[`electron-packager`](https://github.com/maxogden/electron-packager):

```
electron-packager target/ AppName --platform=darwin --arch=x64 --version=0.31.2
```

