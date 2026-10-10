# Minecraft
Minecraft modpacks

## Mods
* [`arcane-bestiary/`](arcane-bestiary/) - Fabric mod for Minecraft 1.21.1 adding new mobs, three bosses, crystalite tools and armor, legendary weapons, and solar panels.

## Modpack layout

- `manifest.json` – CurseForge modpack manifest (Minecraft version, mod loader, mod list).
- `overrides/` – configs, scripts, resource packs etc. copied into the instance on install.
- `scripts/build.sh` – builds `dist/<name>-<version>.zip` (needs `jq` and `zip`).
- `.github/workflows/release.yml` – builds and uploads the pack to CurseForge.

### Adding mods

Each entry in `manifest.json` `files` needs the CurseForge project and file IDs:

```json
{ "projectID": 306612, "fileID": 4973771, "required": true }
```

Find them on the mod's CurseForge page (*About Project* sidebar for the project ID; the file's URL
for the file ID). Alternatively, build the pack in the CurseForge app, use *Export Profile*, and
copy the exported `manifest.json` and `overrides/` into this repo.

Set the Minecraft version and loader in `manifest.json` (`minecraft.version`, `minecraft.modLoaders[].id`,
e.g. `forge-47.2.0`, `neoforge-21.1.1`, `fabric-0.15.11`). If you change the Minecraft version, the
release workflow picks it up automatically.

### Connecting to CurseForge

1. Create the project at <https://authors.curseforge.com> (*Create a project* → Minecraft → Modpacks).
   Upload the first release by hand if CurseForge requires one before the API works.
2. Create an API token at <https://legacy.curseforge.com/account/api-tokens>.
3. In the GitHub repo settings:
   - *Secrets and variables → Actions → Secrets*: add `CURSEFORGE_TOKEN`.
   - *Secrets and variables → Actions → Variables*: add `CURSEFORGE_PROJECT_ID` (shown on your project's page).
4. Release by pushing a tag (`git tag v0.1.0 && git push origin v0.1.0`) or by running the
   *Release to CurseForge* workflow manually.

To build locally: `bash scripts/build.sh`.
