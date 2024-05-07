/*
 * Copyright (C) 2024 Kevin Buzeau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
  * 이 프로그램은 무료 소프트웨어이므로 재배포 및/또는 수정이 가능합니다.
  * GNU 일반 공중 사용 허가서(GNU General Public License)의 조건에 따릅니다.
  * 자유 소프트웨어 재단(라이센스 버전 3) 또는
  * (귀하의 선택에 따라) 이후 버전.
  *
  * 본 프로그램은 유용하게 활용되길 바라는 마음으로 배포되며,
  * 그러나 어떠한 보증도 제공되지 않습니다. 묵시적인 보증도 없이
  * 특정 목적에 대한 상품성 또는 적합성. 참조
  * 자세한 내용은 GNU 일반 공중 라이선스를 참조하세요.
  *
  * GNU General Public License 사본을 받았어야 합니다.
  * 이 프로그램과 함께. 그렇지 않다면 <http://www.gnu.org/licenses/>를 참조하세요.
 */

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
include(":parameters")
include(":source-download")
