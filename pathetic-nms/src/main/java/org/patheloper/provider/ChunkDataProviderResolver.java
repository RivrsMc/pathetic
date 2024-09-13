package org.patheloper.provider;

import org.patheloper.api.snapshot.ChunkDataProvider;
import org.patheloper.provider.paper.PaperChunkDataProvider;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ChunkDataProviderResolver {

  private final ChunkDataProvider chunkDataProvider;

  public ChunkDataProviderResolver(int major, int minor) {
    String version = "1." + major + "." + minor;

    if (isPaper()) {
      chunkDataProvider = new PaperChunkDataProvider();
    } else {
      log.warn("Unsupported server software, please use Paper.");
      throw new IllegalStateException("Unsupported version: " + version);
    }

    log.debug(
      "Detected version v{}, using {}", version, chunkDataProvider.getClass().getSimpleName());
  }

  private boolean isPaper() {
    try {
      Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
